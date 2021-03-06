/**
 *
 * Copyright 2015 Martin Goellnitz
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
import org.tangram.components.editor.EditingHandler

log.info "starting"

log.info "configuring editing handler"
EditingHandler editingHandler = new EditingHandler()
editingHandler.deleteMethodEnabled=(config.getProperty("editor.allows.delete", "true").equals("true"))
module.bind(EditingHandler.class).toInstance(editingHandler)

log.info "done."
println "editor done."
