/**
 *
 * Copyright 2016-2017 Martin Goellnitz
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
package org.tangram.test.objectify.content;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import java.util.ArrayList;
import java.util.List;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.objectify.ObjectifyContent;

/**
 * Persistent bean class just for test purposes.
 */
@Entity
public class BaseClass extends ObjectifyContent implements BaseInterface {

    private String title;

    private List<Ref<BaseClass>> peers;


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
        List<BaseInterface> result = new ArrayList<>(peers.size());
        for (Ref<? extends BaseInterface> r : peers) {
            result.add(r.get());
        }
        return result;
    }


    public void setPeers(List<BaseClass> peers) {
        List<Ref<BaseClass>> result = new ArrayList<>(peers.size());
        for (BaseClass bc : peers) {
            result.add(Ref.create(bc));
        }
        this.peers = result;
    }

} // BaseClass
