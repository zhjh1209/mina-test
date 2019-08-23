package com.test.mina;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class MinaTimeTest {
    private static final int PORT = 9123;

    public static void main(String[] args) throws IOException {
        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        StxEtxLineProtocolFactory codec = new StxEtxLineProtocolFactory(Charset.forName("GB2312"), 2048, "\002", "\003");

        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(codec));

        //配置事务处理Handler，将请求转由TimeServerHandler处理。
        acceptor.setHandler(new TimeServerHandler());
        //配置Buffer的缓冲区大小
        acceptor.getSessionConfig().setReadBufferSize(2048);
        //设置等待时间，每隔IdleTime将调用一次handler.sessionIdle()方法
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
        //绑定端口
        acceptor.bind(new InetSocketAddress(PORT));
        System.out.println("server started at port " + PORT);
    }



    static class TimeServerHandler extends IoHandlerAdapter {
        private Logger logger = LoggerFactory.getLogger(this.getClass());
        public void exceptionCaught(IoSession session, Throwable cause)
                throws Exception {
            cause.printStackTrace();
        }

        public void messageReceived(IoSession session, Object message)
                throws Exception {

            logger.info("接受消息成功..." + message.toString());
            session.write("接收成功！");
        }

        public void sessionIdle(IoSession session, IdleStatus status)
                throws Exception {
            System.out.println("IDLE ==============" + session.getIdleCount(status));
        }
    }

}