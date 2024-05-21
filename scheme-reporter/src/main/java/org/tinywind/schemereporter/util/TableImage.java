/*
  Copyright (c) 2016, Jeon JaeHyeong (http://github.com/tinywind)
  All rights reserved.
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.tinywind.schemereporter.util;

import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.math3.util.Pair;
import org.jooq.meta.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.*;

public class TableImage {
    private static final Logger log = LoggerFactory.getLogger(TableImage.class);

    public static Map<String, String> relationSvg(List<TableDefinition> tables) {
        final Map<String, String> relationSvg = new HashMap<>();
        for (TableDefinition table : tables) {
            final String cTable = table.getName();
            MutableNode node = createNode(cTable);
            node.add(Color.RED);
            node.add(Attributes.attr("weight", 8));
            node.add(Attributes.attr("fillcolor", "grey75"));

            final Map<String, MutableNode> refer = new HashMap<>();
            final Map<String, MutableNode> referred = new HashMap<>();

            for (ColumnDefinition column : table.getColumns()) {
                for (ForeignKeyDefinition fkey : column.getForeignKeys()) {
                    final String rTable = fkey.getReferencedTable().getName();
                    refer.putIfAbsent(rTable, createReferNode(rTable));
                }

                for (UniqueKeyDefinition ukey : column.getKeys()) {
                    for (ForeignKeyDefinition fkey : ukey.getForeignKeys()) {
                        final String rTable = fkey.getKeyTable().getName();
                        if (referred.get(rTable) == null && !cTable.equals(rTable)) {
                            MutableNode referNode = createReferNode(rTable);
                            referred.put(rTable, referNode.addLink(to(node)));
                        }
                    }
                }
            }

            for (MutableNode rNode : refer.values().stream().distinct().toList()) node = node.addLink(to(rNode));
            final MutableGraph g = mutGraph(cTable).setDirected(true).add(node);
            referred.values().forEach(g::add);

            relationSvg.put(cTable, Graphviz.fromGraph(g.toImmutable()).render(Format.SVG).toString());
        }
        return relationSvg;
    }

    public static String totalRelationSvg(List<TableDefinition> tables) {
        final Map<String, MutableNode> nodeMap = tables.stream().collect(Collectors.toMap(Definition::getName, table -> createReferNode(table.getName())));
        final MutableGraph g = mutGraph("totalRelationSvg").setDirected(true);

        final List<Pair<String, String>> linkedList = new ArrayList<>();
        tables.forEach(table -> table.getColumns().forEach(column -> column.getForeignKeys().forEach(fkey -> {
            if (linkedList.contains(Pair.create(table.getName(), fkey.getReferencedTable().getName()))) return;
            final MutableNode linked = nodeMap.get(fkey.getReferencedTable().getName());
            nodeMap.put(table.getName(), nodeMap.get(table.getName()).addLink(to(linked)));
            linkedList.add(Pair.create(table.getName(), fkey.getReferencedTable().getName()));
        })));

        nodeMap.values().forEach(g::add);
        return Graphviz.fromGraph(g.toImmutable().graphAttr().with(Rank.dir(LEFT_TO_RIGHT))).render(Format.SVG).toString();
    }

    private static MutableNode createNode(String name) {
        final MutableNode node = mutNode(name);
        node.add(Attributes.attr("shape", "box"));
        node.add(Attributes.attr("color", "black"));
        node.add(Attributes.attr("fontname", "Helvetica"));
        node.add(Attributes.attr("fontsize", 10));
        node.add(Attributes.attr("fontcolor", "black"));
        node.add(Attributes.attr("height", 0.2));
        node.add(Attributes.attr("width", 0.4));
        node.add(Attributes.attr("style", "filled"));
        return node;
    }

    private static MutableNode createReferNode(String name) {
        final MutableNode node = createNode(name);
        node.add(Attributes.attr("fillcolor", "white"));
        node.add(Attributes.attr("URL", "#table$" + name));
        return node;
    }

    public static byte[] pngBytesFromSvg(String svg) throws TranscoderException {
        svg = svg.replaceAll("stroke=\"transparent\"", "stroke=\"none\"");

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final TranscoderInput input = new TranscoderInput(new StringReader(svg));
        final TranscoderOutput output = new TranscoderOutput(outputStream);
        final PNGTranscoder converter = new PNGTranscoder();
        converter.transcode(input, output);
        byte[] bytes = outputStream.toByteArray();
        try {
            outputStream.close();
        } catch (IOException e) {
            log.error("Error while closing outputStream", e);
        }
        return bytes;
    }

}
