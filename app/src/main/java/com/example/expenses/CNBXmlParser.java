/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.expenses;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CNBXmlParser {
    private static final String ns = null;
    DatabaseHelper db;

    CNBXmlParser(DatabaseHelper db){
        this.db = db;
    }

    public List<Currency> parse(InputStream in) throws XmlPullParserException, IOException {

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List<Currency> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Currency> entries = new ArrayList<Currency>();

        parser.require(XmlPullParser.START_TAG, ns, "kurzy");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("radek")) {
                entries.add(readEntry(parser));
            	
            } else {
            	parser.next();
            }
        }
        return entries;
    }


  
    private Currency readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, "radek");
        int i = 0;
        String code = parser.getAttributeValue(i++);
        String name = parser.getAttributeValue(i++);
        double amount = Double.parseDouble(parser.getAttributeValue(i++));
        String reteTmp = parser.getAttributeValue(i++);
        reteTmp = reteTmp.replace(",", ".");
        double rate = Double.parseDouble(reteTmp);
        String country = parser.getAttributeValue(i++);
        long id = db.createCurrency(code, name, amount, rate, country);

        parser.next();
            
        return new Currency(id, code, name, amount, rate, country);
    }
  
}
