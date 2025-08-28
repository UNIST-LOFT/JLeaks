private void scanEverythingBelongingToRelationships( Read dataRead, CursorFactory cursors ){
    try (RelationshipScanCursor relationshipScanCursor = cursors.allocateRelationshipScanCursor();
        PropertyCursor propertyCursor = cursors.allocatePropertyCursor()) {
        dataRead.allRelationshipsScan(relationshipScanCursor);
        while (relationshipScanCursor.next()) {
            int typeId = relationshipScanCursor.type();
            relationshipScanCursor.properties(propertyCursor);
            MutableIntSet propertyIds = IntSets.mutable.empty();
            while (propertyCursor.next()) {
                int propertyKey = propertyCursor.propertyKey();
                Value currentValue = propertyCursor.propertyValue();
                Pair<Integer, Integer> key = Pair.of(typeId, propertyKey);
                updateValueTypeInMapping(currentValue, key, relationshipTypeIdANDPropertyTypeIdToValueTypeMapping);
                propertyIds.add(propertyKey);
            }
            propertyCursor.close();
            MutableIntSet oldPropertyKeySet = relationshipTypeIdToPropertyKeysMapping.getOrDefault(typeId, emptyPropertyIdSet);
            // find out which old properties we did not visited and mark them as nullable
            if (!(oldPropertyKeySet == emptyPropertyIdSet)) {
                // we can and need to skip this if we found the empty set
                oldPropertyKeySet.removeAll(propertyIds);
                oldPropertyKeySet.forEach(id -> {
                    Pair<Integer, Integer> key = Pair.of(typeId, id);
                    relationshipTypeIdANDPropertyTypeIdToValueTypeMapping.get(key).setNullable();
                });
            }
            propertyIds.addAll(oldPropertyKeySet);
            relationshipTypeIdToPropertyKeysMapping.put(typeId, propertyIds);
        }
        relationshipScanCursor.close();
    }
}