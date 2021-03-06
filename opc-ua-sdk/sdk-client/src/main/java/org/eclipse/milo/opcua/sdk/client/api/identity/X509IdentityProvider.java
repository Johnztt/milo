package org.eclipse.milo.opcua.sdk.client.api.identity;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.SignatureData;
import org.eclipse.milo.opcua.stack.core.types.structured.UserIdentityToken;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.eclipse.milo.opcua.stack.core.types.structured.X509IdentityToken;
import org.eclipse.milo.opcua.stack.core.util.SignatureUtil;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class X509IdentityProvider implements IdentityProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final X509Certificate certificate;
    private final PrivateKey privateKey;

    public X509IdentityProvider(X509Certificate certificate, PrivateKey privateKey) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    @Override
    public Tuple2<UserIdentityToken, SignatureData> getIdentityToken(EndpointDescription endpoint,
                                                                     ByteString serverNonce) throws Exception {

        UserTokenPolicy tokenPolicy = Arrays.stream(endpoint.getUserIdentityTokens())
            .filter(t -> t.getTokenType() == UserTokenType.Certificate)
            .findFirst().orElseThrow(() -> new Exception("no x509 certificate token policy found"));

        String policyId = tokenPolicy.getPolicyId();

        SecurityPolicy securityPolicy = SecurityPolicy.None;

        String securityPolicyUri = tokenPolicy.getSecurityPolicyUri();

        try {
            if (securityPolicyUri != null && !securityPolicyUri.isEmpty()) {
                securityPolicy = SecurityPolicy.fromUri(securityPolicyUri);
            } else {
                securityPolicyUri = endpoint.getSecurityPolicyUri();
                securityPolicy = SecurityPolicy.fromUri(securityPolicyUri);
            }
        } catch (Throwable t) {
            logger.warn("Error parsing SecurityPolicy for uri={}", securityPolicyUri);
        }

        X509IdentityToken token = new X509IdentityToken(
            policyId,
            ByteString.of(certificate.getEncoded())
        );

        ByteString serverCertificate = endpoint.getServerCertificate();

        byte[] serverCertificateBytes = serverCertificate.bytes();
        if (serverCertificateBytes == null) serverCertificateBytes = new byte[0];

        byte[] serverNonceBytes = serverNonce.bytes();
        if (serverNonceBytes == null) serverNonceBytes = new byte[0];

        byte[] signature = SignatureUtil.sign(
            securityPolicy.getAsymmetricSignatureAlgorithm(),
            privateKey,
            ByteBuffer.wrap(serverCertificateBytes),
            ByteBuffer.wrap(serverNonceBytes)
        );

        SignatureData signatureData = new SignatureData(
            securityPolicy.getAsymmetricSignatureAlgorithm().getUri(),
            ByteString.of(signature)
        );

        return new Tuple2<>(token, signatureData);
    }

}
