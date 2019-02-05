/*
 * Copyright © 2018 Gradiant and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.gradiant.arqueopterix.tcmanager;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class IntIgnoreZeroAdapter extends TypeAdapter<Integer> {
    private static Integer INT_ZERO = Integer.valueOf(0);

    @Override
    public Integer read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return 0;
        }

        return in.nextInt();
    }

    @Override
    public void write(JsonWriter out, Integer data) throws IOException {
        if (data == null || data.equals(INT_ZERO)) {
            out.nullValue();
            return;
        }

        out.value(data.intValue());
    }
}
