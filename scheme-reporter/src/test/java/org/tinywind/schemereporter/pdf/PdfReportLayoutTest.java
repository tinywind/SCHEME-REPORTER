package org.tinywind.schemereporter.pdf;

import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import org.apache.batik.transcoder.TranscoderException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class PdfReportLayoutTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder(new File("target"));

    @Test
    public void tallDiagramUsesTheAvailablePageHeight() throws Exception {
        File output = temporary.newFile("tall.pdf");
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='20' height='1100'>"
                + "<rect width='20' height='1100'/></svg>";
        new PdfReporter().writeReport(output, List.of(), List.of(), svg, Map.of());

        List<Float> heights = new ArrayList<>();
        PdfCanvasProcessor processor = new PdfCanvasProcessor(new IEventListener() {
            @Override
            public void eventOccurred(IEventData event, EventType type) {
                heights.add(((ImageRenderInfo) event).getImageCtm().get(Matrix.I22));
            }

            @Override
            public Set<EventType> getSupportedEvents() {
                return Set.of(EventType.RENDER_IMAGE);
            }
        });
        try (PdfDocument pdf = new PdfDocument(new PdfReader(output))) {
            for (int page = 1; page <= pdf.getNumberOfPages(); page++) {
                processor.reset();
                processor.processPageContent(pdf.getPage(page));
            }
        }
        assertEquals(1, heights.size());
        assertTrue(heights.toString(), heights.getFirst() > PageSize.A4.getWidth() - 60);
        assertTrue(heights.toString(), heights.getFirst() <= PageSize.A4.getHeight() - 60);
    }

    @Test
    public void diagramFailureStillFinalizesThePdf() throws Exception {
        File output = temporary.newFile("invalid-diagram.pdf");
        assertThrows(TranscoderException.class,
                () -> new PdfReporter().writeReport(output, List.of(), List.of(), "<svg", Map.of()));
        try (PdfDocument pdf = new PdfDocument(new PdfReader(output))) {
            assertTrue(pdf.getNumberOfPages() > 0);
            assertFalse(pdf.getReader().hasRebuiltXref());
        }
    }
}
