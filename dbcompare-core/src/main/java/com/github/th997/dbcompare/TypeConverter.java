package com.github.th997.dbcompare;

import com.github.th997.dbcompare.bean.TableColumn;

import java.util.ArrayList;
import java.util.List;

public interface TypeConverter {
    TableColumn convert(TableColumn c);

    default List<TableColumn> convert(List<TableColumn> columnList) {
        List<TableColumn> ret = new ArrayList<>(columnList.size());
        for (TableColumn c : columnList) {
            ret.add(convert(c));
        }
        return ret;
    }
}