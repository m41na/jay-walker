package works.hop.json.asmdemo.beans;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;

public class CShape {

    public static void main(String[] args) throws IOException {
        ClassReader cr = new ClassReader(Contact.class.getName());
        ClassWriter cw = new ClassWriter(cr, COMPUTE_FRAMES);

        cr.accept(cw, 0);
    }
}
