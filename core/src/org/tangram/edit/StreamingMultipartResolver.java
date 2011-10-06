/**
 * 
 * Copyright 2009
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.edit;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

public class StreamingMultipartResolver implements MultipartResolver {

	private long maxUploadSize = 50000L;

	public long getMaxUploadSize() {
		return maxUploadSize;
	}

	public void setMaxUploadSize(long maxUploadSize) {
		this.maxUploadSize = maxUploadSize;
	}

	@Override
	public boolean isMultipart(HttpServletRequest request) {
		return ServletFileUpload.isMultipartContent(request);
	}

	@Override
	public MultipartHttpServletRequest resolveMultipart(
			HttpServletRequest request) throws MultipartException {
		ServletFileUpload upload = new ServletFileUpload();
		upload.setFileSizeMax(maxUploadSize);
		String encoding = determineEncoding(request);
		Map<String, String[]> multipartParameters = new HashMap<String, String[]>();
		MultiValueMap<String, MultipartFile> multipartFiles = new LinkedMultiValueMap<String, MultipartFile>();

		try {
			FileItemIterator iter = upload.getItemIterator(request);
			while (iter.hasNext()) {
				FileItemStream item = iter.next();

				String name = item.getFieldName();
				InputStream stream = item.openStream();
				if (item.isFormField()) {
					String value = Streams.asString(stream, encoding);
					String[] curParam = multipartParameters.get(name);
					if (curParam == null) {
						// simple form field
						multipartParameters.put(name, new String[] { value });
					} else {
						// array of simple form fields
						String[] newParam = StringUtils.addStringToArray(
								curParam, value);
						multipartParameters.put(name, newParam);
					}
				} else {
					MultipartFile file = new StreamingMultipartFile(item);
					multipartFiles.add(name, file);
				} // if
			} // while
		} catch (IOException e) {
			throw new MultipartException("something went wrong here", e);
		} catch (FileUploadException e) {
			throw new MultipartException("something went wrong here", e);
		} // try/catch

		return new DefaultMultipartHttpServletRequest(request, multipartFiles,
				multipartParameters);
	} // resolveMultipart()

	@Override
	public void cleanupMultipart(MultipartHttpServletRequest request) {
	}

	/**
	 * Determine the encoding for the given request. Can be overridden in
	 * subclasses.
	 * <p>
	 * The default implementation checks the request encoding, falling back to
	 * the default encoding specified for this resolver.
	 * 
	 * @param request
	 *            current HTTP request
	 * @return the encoding for the request (never <code>null</code>)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding
	 */
	protected String determineEncoding(HttpServletRequest request) {
		String encoding = request.getCharacterEncoding();
		if (encoding == null) {
			encoding = "UTF-8";
		} // if
		return encoding;
	} // determineEncoding()

} // StreamingMultipartResolver