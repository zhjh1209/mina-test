package com.test.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class StxEtxLineEncoder implements ProtocolEncoder {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Charset charset;
    private byte[] stxBytes;
    private byte[] etxBytes;
    public StxEtxLineEncoder(Charset charset, String stx, String etx) {
        this.charset = charset;
        this.stxBytes = stx.getBytes(charset);
        this.etxBytes = etx.getBytes(charset);
    }

    public void encode(IoSession session, Object o, ProtocolEncoderOutput protocolEncoderOutput)
            throws Exception {
        if (o instanceof String) {
            byte[] bytes = ((String)o).getBytes(charset);
            IoBuffer buffer = IoBuffer.allocate(stxBytes.length + bytes.length + etxBytes.length);
            buffer.put(stxBytes).put(bytes).put(etxBytes);
            buffer.flip();
            protocolEncoderOutput.write(buffer);
        } else {
            logger.error("unknown message format, must be string");
        }
    }

    public void dispose(IoSession session) throws Exception {
        // noting to do.
    }
}
