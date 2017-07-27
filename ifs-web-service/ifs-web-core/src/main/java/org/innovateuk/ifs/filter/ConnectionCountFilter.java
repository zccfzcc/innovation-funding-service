package org.innovateuk.ifs.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.GenericFilterBean;

@Service
public class ConnectionCountFilter extends GenericFilterBean {
    private static final Log LOG = LogFactory.getLog(ConnectionCountFilter.class);

    private int count = 0;
    //@Value("${ifs.web.rest.connections.max.total}")
    private int max = 2;


    @Override public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        try {
            count++;
            chain.doFilter(request, response);
        } finally {
            count--;
        }
    }

    public boolean canAcceptConnection(){
        boolean healthy = max > count;

        LOG.debug("incoming connection used = " + count + "/" + max + " healthy = "+ healthy);

        return healthy;
    }
}
