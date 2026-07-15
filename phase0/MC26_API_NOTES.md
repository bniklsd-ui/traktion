---
status: frozen
purpose: Recherche-Notizen zu MC 26.2 Entity/Persistenz-API für P0.4-Spike. Klärt [VERIFY]-Marken: Entity-Registrierung, SavedData-API, ValueOutput/ValueInput.
read-when: vor P0.4-Spike-Code; bei Fragen zur 26.2 Entity- oder Persistenz-API
detail: L2
up: ./CLAUDE.md
down:
related: ./Fabric_Loom_Mappings_Fix_01.md
updated: 2026-07-15
---

# MC 26.2 Entity- & Persistenz-API — Recherche-Notizen

> Quelle: docs.fabricmc.net (Version 26.1.2, Stand 2026-07-15). MC 26.x ist unobfuskiert,
> Mojang-Namen direkt. 1.21.x-Tutorials sind veraltet — diese Notizen sind gegen die
> offizielle 26.1.2-Doku geprüft.

## Entity-Registrierung (26.x)

```java
public class ModEntityTypes {
    public static final EntityType<MyEntity> MY_ENTITY = register(
        "my_entity",
        EntityType.Builder.<MyEntity>of(MyEntity::new, MobCategory.MISC)
            .sized(0.75f, 1.75f)
    );

    private static <T extends Entity> EntityType<T> register(
            String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MOD_ID, name)
        );
        return Registry.register(
            BuiltInRegistries.ENTITY_TYPE, key, builder.build(key)
        );
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(
            MY_ENTITY, MyEntity.createAttributes()
        );
    }
}
```

**Änderungen zu 1.21.x:**
- `Registry.register(BuiltInRegistries.ENTITY_TYPE, key, ...)` statt `Registries.ENTITY_TYPE`
  (Name kann abweichen — in 26.x ist es `BuiltInRegistries`)
- `ResourceKey.create` statt `Identifier.of` in einigen Kontexten
- `Identifier.fromNamespaceAndPath(modId, name)` statt `Identifier.of(modId, name)`

## Entity-NBT-Persistenz (26.x)

**API-Änderung:** `writeNbt`/`readNbt` mit `NbtCompound` → `addAdditionalSaveData`/`readAdditionalSaveData` mit `ValueOutput`/`ValueInput`.

```java
@Override
protected void addAdditionalSaveData(ValueOutput valueOutput) {
    super.addAdditionalSaveData(valueOutput);
    valueOutput.putInt("my_field", this.myField);
}

@Override
protected void readAdditionalSaveData(ValueInput valueInput) {
    super.readAdditionalSaveData(valueInput);
    this.myField = valueInput.getInt("my_field").orElse(0);
}
```

**Wichtig:** `ValueInput.getInt()` gibt `Optional<Integer>` zurück, nicht `int`.
`NbtCompound` wird in Entity-Persistenz nicht mehr direkt verwendet.

## Welt-attached Persistent State (T-D15 — geklärt)

**API:** `SavedData` + `SavedDataType` + `level.getDataStorage().computeIfAbsent(TYPE)`

```java
public class MySavedData extends SavedData {
    private static final Codec<MySavedData> CODEC = Codec.INT.xmap(
        MySavedData::new,
        MySavedData::getValue
    );

    private static final SavedDataType<MySavedData> TYPE = new SavedDataType<>(
        "my_data_file",  // Dateiname in world/data/
        MySavedData::new,
        CODEC
    );

    public static MySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    @Override
    public boolean isDirty() {
        return true; // oder eigene Dirty-Logik
    }
}
```

**Änderungen zu 1.21.x:**
- `PersistentState` → `SavedData`
- `ServerLevel.getDataStorage().computeIfAbsent(TYPE)` statt
  `PersistentState.getType()`-Factory-Muster
- Codec-basiert statt manuellem `writeNbt`/`readFromNbt`
- `SavedDataType` ist ein Record/Class, der Name + Factory + Codec bündelt

**Für T-D15:** Der Graph liegt als welt-attached SavedData pro Dimension. `train-core`
liefert ein `GraphSnapshot`-Record, `train-mc` kodiert es via Codec in `SavedData`.

## Despawn / Rekonstruktion bei Spielerentfernung

**Noch ungeklärt [VERIFY]:** Die Fabric-Doku behandelt nicht explizit, wie Entities bei
Spielerentfernung despawnen und zustandserhaltend rekonstruiert werden. Das ist
Vanilla-Verhalten (Chunk-Unload → Entity wird gespeichert, Chunk-Load → Entity wird
rekonstruiert). Für den Spike muss das manuell getestet werden.

**Hypothese:** Wenn die Entity korrekt `addAdditionalSaveData`/`readAdditionalSaveData`
implementiert, speichert Vanilla sie beim Chunk-Unload und rekonstruiert sie beim
Chunk-Load mit erhaltenem Zustand. Der Spike muss das bestätigen.

## jqwik [VERIFY — noch offen]

Nicht in dieser Recherche geklärt. P0.4 oder P1 muss es testen.
