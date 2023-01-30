package io.confluent.examplex.pcsample;

import java.io.IOException;

public class PCSampleProgram {

    public static void main(String ... args) throws IOException {
        PcSampleService pcSampleService = new PcSampleService();
        // TODO: what about clean shutdown?
        pcSampleService.run();
    }

}

