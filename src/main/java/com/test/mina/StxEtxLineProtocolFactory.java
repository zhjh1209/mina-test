package com.test.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import java.nio.charset.Charset;

public class StxEtxLineProtocolFactory implements ProtocolCodecFactory {
    private final Charset charset;
    private final int bufferLength;
    private final String stx;
    private final String etx;
    public StxEtxLineProtocolFactory(Charset charset, int bufferLength, String stx, String etx) {
        if (charset == null) {
            throw new IllegalArgumentException("charset must been set");
        }
        this.charset = charset;
        this.bufferLength = bufferLength;
        this.stx = stx;
        this.etx = etx;
    }
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return new StxEtxLineEncoder(charset, stx, etx);
    }

    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return new StxEtxLineDecoder(charset, stx, etx, bufferLength);
    }
}
