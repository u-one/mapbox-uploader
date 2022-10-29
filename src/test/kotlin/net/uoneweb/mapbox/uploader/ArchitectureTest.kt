package net.uoneweb.mapbox.uploader

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test

class ArchitectureTest {
    @Test
    fun repositoryInterfacesShouldNotInRepositorylayer() {
        val importedClasse = ClassFileImporter().importPackages("net.uoneweb.mapbox")

        val rule = classes().that().resideInAPackage("..repository..").should().notBeInterfaces()

        rule.check(importedClasse)
    }
}