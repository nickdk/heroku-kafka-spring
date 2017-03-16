package com.xti.nickdk.kafka;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class TemporaryKeyStore {
    private String password;
    private KeyStore keystore;

    public static TemporaryKeyStore createWithRandomPassword(String key, String cert) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        return new TemporaryKeyStore(key, cert, (new BigInteger(130, new SecureRandom())).toString(32));
    }

    public static TemporaryKeyStore createWithRandomPassword(String cert) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        return new TemporaryKeyStore(cert, (new BigInteger(130, new SecureRandom())).toString(32));
    }

    TemporaryKeyStore(String key, String cert, String password) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        this.password = password;
        this.keystore = createKeyStore(new StringReader(key), new StringReader(cert), password);
    }

    TemporaryKeyStore(String cert, String password) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        this.password = password;
        this.keystore = createTrustStore(new StringReader(cert));
    }

    public String password() {
        return this.password;
    }

    public String type() {
        return "PKCS12";
    }

    public byte[] toBytes() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        this.store(bos);
        bos.close();
        return bos.toByteArray();
    }

    public void store(OutputStream out) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        this.keystore.store(out, this.password.toCharArray());
    }

    public void store(Path path) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        Files.write(path, this.toBytes());
    }

    public File storeTemp() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        File temp = File.createTempFile("env-keystore", this.type().toLowerCase());
        this.store(temp.toPath());
        return temp;
    }

    private static KeyStore createKeyStore(Reader keyReader, Reader certReader, String password) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        PEMParser pem = new PEMParser(keyReader);
        PEMKeyPair pemKeyPair = (PEMKeyPair)pem.readObject();
        JcaPEMKeyConverter jcaPEMKeyConverter = new JcaPEMKeyConverter();
        KeyPair keyPair = jcaPEMKeyConverter.getKeyPair(pemKeyPair);
        PrivateKey key = keyPair.getPrivate();
        pem.close();
        keyReader.close();
        X509Certificate certificate = parseCert(certReader);
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null);
        ks.setKeyEntry("alias", key, password.toCharArray(), new X509Certificate[]{certificate});
        return ks;
    }

    private static KeyStore createTrustStore(Reader certReader) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        X509Certificate certificate = parseCert(certReader);
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null);
        ks.setCertificateEntry("alias", certificate);
        return ks;
    }

    private static X509Certificate parseCert(Reader certReader) throws IOException, CertificateException {
        PEMParser pem = new PEMParser(certReader);
        X509CertificateHolder certHolder = (X509CertificateHolder)pem.readObject();
        X509Certificate certificate = (new JcaX509CertificateConverter()).getCertificate(certHolder);
        pem.close();
        return certificate;
    }
}
