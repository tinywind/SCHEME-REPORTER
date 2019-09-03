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

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import javafx.util.Pair;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.jooq.util.Definition;
import org.jooq.util.TableDefinition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

public class TableImage {
    public static Map<String, String> relationSvg(List<TableDefinition> tables) {
        final Map<String, String> relationSvg = new HashMap<>();
        for (TableDefinition table : tables) {
            final String cTable = table.getName();
            final Node[] cNode = {createNode(cTable).attr("weight", 8).attr("fillcolor", "grey75")};

            final Map<String, Node> refer = new HashMap<>();
            final Map<String, Node> referred = new HashMap<>();

            table.getColumns().forEach(column -> {
                column.getForeignKeys().forEach(fkey -> {
                    final String rTable = fkey.getReferencedTable().getName();
                    refer.putIfAbsent(rTable, createReferNode(rTable));
                });
                column.getUniqueKeys().forEach(ukey -> ukey.getForeignKeys().forEach(fkey -> {
                    final String rTable = fkey.getKeyTable().getName();
                    if (referred.get(rTable) == null && !cTable.equals(rTable))
                        referred.put(rTable, createReferNode(rTable).link(cNode[0]));
                }));
            });

            refer.values().stream().distinct().forEach(rNode -> cNode[0] = cNode[0].link(rNode));
            final Graph g = graph(cTable).directed().node(cNode[0]);
            referred.values().stream().distinct().forEach(g::node);

            relationSvg.put(cTable, Graphviz.fromGraph(g).createSvg());
        }
        return relationSvg;
    }

    public static String totalRelationSvg(List<TableDefinition> tables) {
        final Map<String, Node> nodeMap = tables.stream().collect(Collectors.toMap(Definition::getName, table -> createReferNode(table.getName())));
        final Graph g = graph("totalRelationSvg").directed()
                .general().attr(RankDir.LEFT_TO_RIGHT);

        final List<Pair<String, String>> linkedList = new ArrayList<>();
        tables.forEach(table -> table.getColumns().forEach(column -> column.getForeignKeys().forEach(fkey -> {
            if (linkedList.contains(new Pair<>(table.getName(), fkey.getReferencedTable().getName()))) return;
            final Node linked = nodeMap.get(fkey.getReferencedTable().getName());
            nodeMap.put(table.getName(), nodeMap.get(table.getName()).link(linked));
            linkedList.add(new Pair<>(table.getName(), fkey.getReferencedTable().getName()));
        })));

        nodeMap.forEach((name, node) -> g.node(node));
        return Graphviz.fromGraph(g).createSvg();
    }

    private static Node createNode(String name) {
        return node(name).attr(Shape.RECTANGLE).attr(Color.BLACK)
                .attr("fontname", "Helvetica").attr("fontsize", 10).attr("fontcolor", "black")
                .attr("height", 0.2).attr("width", 0.4).attr("style", "filled");
    }

    private static Node createReferNode(String name) {
        return createNode(name).attr("fillcolor", "white").attr("URL", "#table$" + name);
    }

    public static byte[] pngBytesFromSvg(String svg) throws TranscoderException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final TranscoderInput input = new TranscoderInput(new StringReader(svg));
        final TranscoderOutput output = new TranscoderOutput(outputStream);
        final PNGTranscoder converter = new PNGTranscoder();
        converter.transcode(input, output);
        byte[] bytes = outputStream.toByteArray();
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

}
