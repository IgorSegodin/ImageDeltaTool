package org.segodin.imageDeltaTool.service;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.segodin.imageDeltaTool.service.data.BufferedImageData;
import org.segodin.imageDeltaTool.service.data.HighlightZone;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class ImageDiffServiceImplTest {

    private ImageDiffServiceImpl service;

    @Before
    public void before() {
        service = EasyMock.createMockBuilder(ImageDiffServiceImpl.class)
                .addMockedMethod("highlightImage")
                .createMock();

    }

    @Test
    public void testDifferentZonesCount() throws URISyntaxException, IOException {
        BufferedImage originImage = ImageIO.read(new File(this.getClass().getClassLoader().getResource("rabbit_01.jpg").toURI()));
        BufferedImage toCompareImage = ImageIO.read(new File(this.getClass().getClassLoader().getResource("rabbit_02.jpg").toURI()));

        Capture<List<HighlightZone>> capturedHighlightZones = EasyMock.newCapture();

        EasyMock.expect(service.highlightImage(EasyMock.capture(capturedHighlightZones), EasyMock.anyObject(BufferedImage.class))).andReturn(null);
        EasyMock.replay(service);

        service.getHighlightedImage(new BufferedImageData(originImage), new BufferedImageData(toCompareImage));

        Assert.assertEquals(5, capturedHighlightZones.getValue().size());
    }
}
