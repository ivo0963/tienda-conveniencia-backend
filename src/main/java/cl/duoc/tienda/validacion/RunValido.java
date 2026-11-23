package cl.duoc.tienda.validacion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RunValido.Validador.class)
public @interface RunValido {

    String message() default "RUN inválido. Escríbelo sin puntos ni guion (ej: 123456785).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validador implements ConstraintValidator<RunValido, String> {
        @Override
        public boolean isValid(String valor, ConstraintValidatorContext contexto) {
            // Si viene vacío lo reporta @NotBlank
            return valor == null || valor.isBlank() || Validaciones.runValido(valor);
        }
    }
}
