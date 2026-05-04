package com.vomiter.recyclingarrows.common.arrow.data;

import java.util.List;

public interface IArrowRecordHolder {

    void addArrow(StoredArrowStack arrow);

    StoredArrow removeArrow();

    StoredArrow removeArrow(StoredArrow arrow);

    List<StoredArrowStack> getArrows();

    void clear();
}