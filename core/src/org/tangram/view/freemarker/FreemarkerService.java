/*
 *
 * Copyright 2019 Martin Goellnitz
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
package org.tangram.view.freemarker;

import freemarker.template.Configuration;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;


/**
 * Service to setup freemarker templating within the framework.
 */
@Named
@Singleton
public class FreemarkerService {

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        // TODO: how to ensure that this configuration gets used.
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);

        IncludeDirective includeDirective = new IncludeDirective();
        configuration.setSharedVariable("incude", includeDirective);

        LinkDirective linkDirective = new LinkDirective();
        configuration.setSharedVariable("link", linkDirective);
    }

}
