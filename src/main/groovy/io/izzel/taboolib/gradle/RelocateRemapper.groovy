package io.izzel.taboolib.gradle

import groovy.transform.Canonical
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper

@Canonical
class RelocateRemapper extends Remapper {

    Map<String, String> dot
    Map<String, String> slash
    Map<String, Set<String>> use = new TreeMap()
    ClassRemapper remapper

    @Override
    Object mapValue(Object value) {
        if (value instanceof String) {
            def match = dot.find { (value as String).startsWith(it.key) }
            if (match) {
                return (match.value + (value as String).substring(match.key.length())).toString()
            }
        }
        return super.mapValue(value)
    }

    @SuppressWarnings('GroovyAccessibility')
    @Override
    String map(String internalName) {
        if (remapper != null) {
            use.computeIfAbsent(remapper.className) { new HashSet() }.add(internalName)
        }
        if (internalName.startsWith('kotlin/Metadata')) {
            return internalName
        }
        def match = slash.find { internalName.startsWith(it.key) }
        if (match) {
            if (match.value.startsWith("!")) {
                def index = internalName.lastIndexOf('/')
                return match.value.substring(1) + "/" + internalName.substring(index + 1, internalName.length())
            }
            return match.value + internalName.substring(match.key.length())
        } else {
            return internalName
        }
    }
}
