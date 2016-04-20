/**
 *
 * Copyright 2016 Martin Goellnitz
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
package org.tangram.morphia.test.content;

import java.util.List;
import org.mongodb.morphia.annotations.Entity;
import org.tangram.morphia.MorphiaContent;
import org.tangram.mutable.test.content.BaseInterface;

/**
 * Persistent bean class just for test purposes.
 */
@Entity
public class BaseClass extends MorphiaContent implements BaseInterface {

    private String title;

    private List<BaseClass> peers;


    @Override
    public String getTitle() {
        return title;
    }


    @Override
    public void setTitle(String title) {
        this.title = title;
    }


    @Override
    public List<? extends BaseInterface> getPeers() {
        return peers;
    }


    public void setPeers(List<BaseClass> peers) {
        this.peers = peers;
    }

} // BaseClass
