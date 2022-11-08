package eu.essi_lab.vlab.web.api.servlet.filter;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import java.io.IOException;

/**
 * @author Mattia Santoro
 */
public class ContentTypeFixInterceptor implements ReaderInterceptor {

	private static final String CONTENT_TYPE = "Content-Type";

	@Override
	public Object aroundReadFrom(ReaderInterceptorContext readerInterceptorContext) throws IOException, WebApplicationException {
		MultivaluedMap<String, String> m = readerInterceptorContext.getHeaders();
		if (!m.containsKey(CONTENT_TYPE) || null == m.get(CONTENT_TYPE)) {
			String ct = null;
			//			Exchange exchange = m.getExchange();
			//			if (exchange != null) {
			//				ct = (String) exchange.get(CONTENT_TYPE);
			//			}
			//
			//			m.put(CONTENT_TYPE, ct);
		}

		return readerInterceptorContext.proceed();
	}
}
