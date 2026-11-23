package cl.duoc.tienda.validacion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ValidacionesTest {

    @Test
    void aceptaRunConDigitoVerificadorCorrecto() {
        assertThat(Validaciones.runValido("123456785")).isTrue();
        assertThat(Validaciones.runValido("12.345.678-5")).isTrue();
        assertThat(Validaciones.runValido("10000013k")).isTrue();
        assertThat(Validaciones.runValido("111111111")).isTrue();
    }

    @Test
    void rechazaRunConDigitoIncorrectoOFormatoInvalido() {
        assertThat(Validaciones.runValido("123456780")).isFalse();
        assertThat(Validaciones.runValido("12345")).isFalse();
        assertThat(Validaciones.runValido("abcdefghi")).isFalse();
        assertThat(Validaciones.runValido(null)).isFalse();
    }

    @Test
    void normalizaRunQuitandoPuntosYGuion() {
        assertThat(Validaciones.normalizarRun("10.000.013-k")).isEqualTo("10000013K");
    }

    @Test
    void soloPermiteDominiosDeLaInstitucionYGmail() {
        assertThat(Validaciones.dominioPermitido("alumno@duoc.cl")).isTrue();
        assertThat(Validaciones.dominioPermitido("PROFE@profesor.duoc.cl")).isTrue();
        assertThat(Validaciones.dominioPermitido("persona@gmail.com")).isTrue();
        assertThat(Validaciones.dominioPermitido("persona@hotmail.com")).isFalse();
    }
}
