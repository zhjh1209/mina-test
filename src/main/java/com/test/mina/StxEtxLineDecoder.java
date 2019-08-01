package com.test.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;

public class StxEtxLineDecoder implements ProtocolDecoder {
    private static AttributeKey CONTEXT = new AttributeKey(StxEtxLineDecoder.class, "context");
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Charset charset;
    private byte[] stxBytes;
    private byte[] etxBytes;
    private int bufferLength;

    public StxEtxLineDecoder(Charset charset, String stx, String etx, int bufferLength) {
        if (bufferLength <= 0) {
            throw new IllegalArgumentException("bufferLength must be a positive value");
        }
        this.charset = charset;
        this.stxBytes = stx.getBytes(charset);
        this.etxBytes = etx.getBytes(charset);
        this.bufferLength = bufferLength;
    }

    public void decode(IoSession session, IoBuffer ioBuffer,
            ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
        Context context = getContext(session);
        byte[] startBuffer = new byte[stxBytes.length];
        byte[] endBuffer = new byte[etxBytes.length];
        while (ioBuffer.hasRemaining()) {
            byte read = ioBuffer.get();
            if (!context.start) {
                context.stxBuffer.put(read);
                if (context.stxBuffer.position() == startBuffer.length) {
                    context.stxBuffer.flip();
                    Arrays.fill(startBuffer, (byte)0);
                    context.stxBuffer.get(startBuffer);
                    if (Arrays.equals(startBuffer, stxBytes)) {
                        context.start = true;
                        context.stxBuffer.clear();
                    } else {
                        context.stxBuffer.clear();
                        context.stxBuffer.put(startBuffer,1, startBuffer.length - 1);
                    }
                }
            } else {
                context.etxBuffer.put(read);
                if (context.etxBuffer.position() == endBuffer.length) {
                    context.etxBuffer.flip();
                    Arrays.fill(endBuffer, (byte) 0);
                    context.etxBuffer.get(endBuffer);
                    if (Arrays.equals(endBuffer, etxBytes)) {
                        context.start = false;
                        int length = context.buffer.position() - etxBytes.length + 1;
                        context.buffer.flip();
                        byte[] content = new byte[length];
                        context.buffer.get(content);
                        protocolDecoderOutput.write(new String(content, charset));
                        context.buffer.clear();
                        context.etxBuffer.clear();
                        continue;
                    } else {
                        context.etxBuffer.clear();
                        context.etxBuffer.put(endBuffer,1, startBuffer.length - 1);
                    }
                }
                if (context.buffer.position() > bufferLength - stxBytes.length - 1) {
                    context.start = false;
                    context.buffer.clear();
                    context.etxBuffer.clear();
                    logger.error("message too long， drop it");
                    continue;
                }
                context.buffer.put(read);
            }
        }
    }

    public void finishDecode(IoSession session, ProtocolDecoderOutput protocolDecoderOutput)
            throws Exception {
        // nothing to do.
    }

    private Context getContext(IoSession session) {
        Context ctx;
        ctx = (Context) session.getAttribute(CONTEXT);
        if (ctx == null) {
            ctx = new Context(bufferLength);
            session.setAttribute(CONTEXT, ctx);
        }
        return ctx;
    }

    public void dispose(IoSession session) throws Exception {
        Context ctx = (Context) session.getAttribute(CONTEXT);
        if (ctx != null) {
            ctx.buffer.free();
            ctx.stxBuffer.free();
            ctx.etxBuffer.free();
            session.removeAttribute(CONTEXT);
        }
    }

    private class Context {
        IoBuffer buffer;
        IoBuffer stxBuffer;
        IoBuffer etxBuffer;
        boolean start;

        Context(int bufferLength) {
            buffer = IoBuffer.allocate(bufferLength);
            stxBuffer = IoBuffer.allocate(stxBytes.length);
            etxBuffer = IoBuffer.allocate(etxBytes.length);
        }
    }
}
