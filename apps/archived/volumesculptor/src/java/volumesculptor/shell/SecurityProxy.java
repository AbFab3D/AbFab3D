/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package volumesculptor.shell;

import abfab3d.grid.Model;
import abfab3d.util.TriangleMesh;
import org.mozilla.javascript.*;

public abstract class SecurityProxy extends SecurityController
{
    protected abstract Model callProcessFileSecure(Context cx, Scriptable scope,
                                                  String filename, Object[] args);

}
