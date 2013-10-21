/**
 * 
 * Copyright 2011-2013 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
// Minification filter classes for JavaScript and CSS
class JavaScriptMinify extends FilterReader {

    Reader inputReader

    public JavaScriptMinify(Reader inputReader) {
        super(inputReader)
        if (inputReader!=null) {
            try {
                com.yahoo.platform.yui.compressor.JavaScriptCompressor jsc = new com.yahoo.platform.yui.compressor.JavaScriptCompressor(inputReader, null)
                StringWriter out = new StringWriter()
                jsc.compress(out, 80, false, false, false, false)
                out.close()
                super.in = new StringReader(out.getBuffer().toString())
            } catch (Exception e) {
                ; // who cares
            } // try/catch
        } // if
    } // JavaScriptMinify()

} // JavaScriptMinify


class CSSMinify extends FilterReader {

    Reader inputReader

    public CSSMinify(Reader inputReader) {
        super(inputReader);
        if (inputReader!=null) {
            try {
                com.yahoo.platform.yui.compressor.CssCompressor csc = new com.yahoo.platform.yui.compressor.CssCompressor(inputReader)
                StringWriter out = new StringWriter()
                csc.compress(out, 0)
                out.close()
                super.in = new StringReader(out.getBuffer().toString())
            } catch (Exception e) {
                ; // who cares
            } // try/catch
        } // if
    } // CSSMinify()

} // CSSMinify
