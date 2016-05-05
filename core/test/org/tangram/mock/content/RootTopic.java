/*
 *
 * Copyright 2011-2015 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.mock.content;

import java.util.List;
import org.tangram.content.TransientCode;


/**
 * Root navigation topic - thus for real site a singleton - for the data model used in tests.
 */
public class RootTopic extends Topic {

    private List<Topic> bottomLinks;

    private List<TransientCode> css;

    private List<TransientCode> js;

    private ImageData logo;


    public List<Topic> getBottomLinks() {
        return this.bottomLinks;
    }


    public void setBottomLinks(List<Topic> bottomLinks) {
        this.bottomLinks = bottomLinks;
    }


    public List<TransientCode> getCss() {
        return this.css;
    }


    public void setCss(List<TransientCode> css) {
        this.css = css;
    }


    public List<TransientCode> getJs() {
        return this.js;
    }


    public void setJs(List<TransientCode> js) {
        this.js = js;
    }


    public ImageData getLogo() {
        return logo;
    }


    public void setLogo(ImageData logo) {
        this.logo = logo;
    }


    @Override
    public RootTopic getRootTopic() {
        return this;
    }

} // RootTopic
