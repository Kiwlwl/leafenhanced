package com.leafenhanced.mod.client.wind;

import net.minecraft.core.SectionPos;
import java.util.HashSet;
import java.util.Set;

public class LeafSectionTracker {
    private static final Set<Long> leafSections = new HashSet<>();
    private static final Set<Long> pendingAdditions = new HashSet<>();

    public static void mark(int x, int y, int z) {
        pendingAdditions.add(SectionPos.asLong(x >> 4, y >> 4, z >> 4));
    }

    public static void flush() {
        leafSections.clear();
        leafSections.addAll(pendingAdditions);
        pendingAdditions.clear();
    }

    public static boolean contains(long sectionKey) {
        return leafSections.contains(sectionKey);
    }
}
