package dk.kingu.pdf;

import org.testng.Assert;
import org.testng.annotations.Test;

import dk.kingu.shooting.ResultFileProcessor;

public class RegexTest {
    
    @Test
    public void testExtract() {
        Assert.assertEquals(-1, ResultFileProcessor.extractLaneNumber(" Vi"));
        Assert.assertEquals(-1, ResultFileProcessor.extractLaneNumber("Vi har modtaget din bestilli"));       
        Assert.assertEquals(9, ResultFileProcessor.extractLaneNumber("9 0"));       

    }
        
}
