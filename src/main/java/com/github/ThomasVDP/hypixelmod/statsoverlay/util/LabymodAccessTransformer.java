package com.github.ThomasVDP.hypixelmod.statsoverlay.util;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public class LabymodAccessTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if (basicClass == null) return null;

        if (transformedName.equals("net.labymod.core_implementation.mc18.gui.ModPlayerTabOverlay")) {
            System.out.println("Found class!");

            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            for (MethodNode n : classNode.methods) {
                System.out.println(n.name + "   " + n.desc);
                if (n.name.equals("func_175245_a") && n.desc.equals("(IIILnet/minecraft/client/network/NetworkPlayerInfo;)V")) {
                    System.out.println("Drawping transformed!");
                    n.access = ACC_PUBLIC;
                }
                if (n.name.equals("drawScoreboardValues") && n.desc.equals("(Lnet/minecraft/scoreboard/ScoreObjective;ILjava/lang/String;IILnet/minecraft/client/network/NetworkPlayerInfo;)V")) {
                    System.out.println("drawScoreboardValues transformed!");
                    n.access = ACC_PUBLIC;
                }
            }

            ClassWriter writer = new ClassWriter(1);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }
}
