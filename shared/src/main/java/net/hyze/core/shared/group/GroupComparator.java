package net.hyze.core.shared.group;

import java.util.Comparator;

public class GroupComparator implements Comparator<Group> {

    @Override
    public int compare(Group g1, Group g2) {
        if (g1.isHigher(g2)) {
            return 1;
        }

        if (g1 == g2) {
            return 0;
        }

        return -1;
    }
}
