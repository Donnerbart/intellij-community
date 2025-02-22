// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.platform.settings.local

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.intellij.util.SystemProperties
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore
import org.h2.mvstore.type.ByteArrayDataType
import org.h2.mvstore.type.StringDataType
import java.nio.file.Files
import java.nio.file.Path

object DevIdea {
  @JvmStatic
  fun main(args: Array<String>) {
    DbConverter.main(arrayOf(SystemProperties.getUserHome() + "/projects/idea/out/dev-data/idea/system/app-cache-settings.db"))
  }
}

// CBOR-encoded values to YAML
// TOML/JSON is not so readable as YAML for large nested data, after evaluation YAML was choosen.
object DbConverter {
  @JvmStatic
  fun main(args: Array<String>) {
    val mvMapBuilder = MVMap.Builder<String, ByteArray>().keyType(StringDataType.INSTANCE).valueType(ByteArrayDataType.INSTANCE)

    for (arg in args) {
      val store = MVStore.Builder().fileName(arg).readOnly().open()
      Files.newBufferedWriter(Path.of("$arg.yaml")).use { fileOut ->
        for (mapName in store.mapNames) {
          fileOut.write("# Map: $mapName\n")
          YAMLFactory().createGenerator(fileOut)
            .configure(com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)
            .useDefaultPrettyPrinter().use { out ->
              val map = store.openMap(mapName, mvMapBuilder)
              out.writeStartObject()
              for ((k, v) in map.entries) {
                val tree = CBORMapper.builder().build().readTree(v)
                val objectMapper = ObjectMapper()
                out.writeFieldName(k)
                objectMapper.writeTree(out, tree)
              }
              out.writeEndObject()
            }
        }
      }
    }
  }
}