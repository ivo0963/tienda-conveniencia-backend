package cl.duoc.tienda.controlador;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

/** Pruebas de la API completa (controlador + seguridad + servicio + base H2) con MockMvc. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegracionTest {

    private static final String API = "/api/v1";

    @Autowired
    private MockMvc mvc;

    private MvcResult login(String correo, String password) throws Exception {
        return mvc.perform(post(API + "/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String token(String correo, String password) throws Exception {
        return JsonPath.read(login(correo, password).getResponse().getContentAsString(), "$.token");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    // ---------- Autenticación ----------

    @Test
    void loginDevuelveTokenYDejaElRefrescoEnUnaCookieHttpOnly() throws Exception {
        mvc.perform(post(API + "/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.rol").value("admin"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist())
                .andExpect(cookie().httpOnly(AuthControlador.COOKIE_REFRESCO, true))
                .andExpect(cookie().path(AuthControlador.COOKIE_REFRESCO, "/api/v1/auth"));
    }

    @Test
    void loginConClaveIncorrectaResponde401() throws Exception {
        mvc.perform(post(API + "/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos."));
    }

    @Test
    void refreshConLaCookieEntregaUnNuevoTokenDeAcceso() throws Exception {
        Cookie refresco = login("juan.perez@gmail.com", "juan1234").getResponse().getCookie(AuthControlador.COOKIE_REFRESCO);

        mvc.perform(post(API + "/auth/refresh").cookie(refresco))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.usuario.correo").value("juan.perez@gmail.com"));
    }

    @Test
    void refreshSinCookieResponde401() throws Exception {
        mvc.perform(post(API + "/auth/refresh")).andExpect(status().isUnauthorized());
    }

    @Test
    void unTokenDeRefrescoNoSirveComoTokenDeAcceso() throws Exception {
        Cookie refresco = login("admin@duoc.cl", "admin123").getResponse().getCookie(AuthControlador.COOKIE_REFRESCO);

        mvc.perform(get(API + "/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(refresco.getValue())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Token inválido o expirado."));
    }

    @Test
    void registroCreaUnClienteYValidaElRun() throws Exception {
        mvc.perform(post(API + "/auth/registro").contentType(MediaType.APPLICATION_JSON).content("""
                        {"run":"201234565","nombre":"Camila","apellidos":"Soto","correo":"camila@gmail.com",
                         "password":"clave1","region":"Biobío","comuna":"Chillán"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("cliente"));

        mvc.perform(post(API + "/auth/registro").contentType(MediaType.APPLICATION_JSON).content("""
                        {"run":"123456780","nombre":"X","apellidos":"Y","correo":"x@yahoo.com",
                         "password":"clave1","region":"Biobío","comuna":"Chillán"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.run").exists())
                .andExpect(jsonPath("$.errores.correo").exists());
    }

    // ---------- Permisos por rol ----------

    @Test
    void elCatalogoEsPublico() throws Exception {
        mvc.perform(get(API + "/productos")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(10)));
        mvc.perform(get(API + "/categorias")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void sinTokenNoSePuedeEntrarAlPanel() throws Exception {
        mvc.perform(get(API + "/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(get(API + "/boletas")).andExpect(status().isUnauthorized());
    }

    @Test
    void elVendedorVeProductosYOrdenesPeroNadaMas() throws Exception {
        String vendedor = token("vendedor@profesor.duoc.cl", "vende123");

        mvc.perform(get(API + "/boletas").header(HttpHeaders.AUTHORIZATION, bearer(vendedor))).andExpect(status().isOk());
        mvc.perform(get(API + "/boletas/1").header(HttpHeaders.AUTHORIZATION, bearer(vendedor))).andExpect(status().isOk());
        mvc.perform(get(API + "/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(vendedor))).andExpect(status().isForbidden());
        mvc.perform(get(API + "/reportes/resumen").header(HttpHeaders.AUTHORIZATION, bearer(vendedor))).andExpect(status().isForbidden());
        mvc.perform(patch(API + "/boletas/2/estado").header(HttpHeaders.AUTHORIZATION, bearer(vendedor))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"enviada\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete(API + "/productos/8").header(HttpHeaders.AUTHORIZATION, bearer(vendedor))).andExpect(status().isForbidden());
    }

    @Test
    void elClienteSoloVeSusPropiasBoletas() throws Exception {
        String cliente = token("juan.perez@gmail.com", "juan1234");

        mvc.perform(get(API + "/boletas").header(HttpHeaders.AUTHORIZATION, bearer(cliente))).andExpect(status().isForbidden());
        mvc.perform(get(API + "/boletas/1").header(HttpHeaders.AUTHORIZATION, bearer(cliente))).andExpect(status().isOk());
        mvc.perform(get(API + "/boletas/2").header(HttpHeaders.AUTHORIZATION, bearer(cliente))).andExpect(status().isForbidden());
        mvc.perform(get(API + "/boletas/mias").header(HttpHeaders.AUTHORIZATION, bearer(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ---------- CRUD (administrador) ----------

    @Test
    void elAdministradorHaceElCrudCompletoDeProductos() throws Exception {
        String admin = bearer(token("admin@duoc.cl", "admin123"));
        String nuevo = """
                {"codigo":"con-001","nombre":"Helado 1 L","precio":3500,"stock":8,"stockCritico":2,"categoriaId":4}""";

        MvcResult creado = mvc.perform(post(API + "/productos").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(nuevo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("CON-001"))
                .andExpect(jsonPath("$.imagen").value("/img/sin-imagen.svg"))
                .andReturn();
        Integer id = JsonPath.read(creado.getResponse().getContentAsString(), "$.id");

        mvc.perform(put(API + "/productos/" + id).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevo.replace("3500", "2990").replace("Helado 1 L", "Helado Vainilla 1 L")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(2990))
                .andExpect(jsonPath("$.nombre").value("Helado Vainilla 1 L"));

        mvc.perform(patch(API + "/productos/" + id + "/stock").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(13));

        mvc.perform(delete(API + "/productos/" + id).header(HttpHeaders.AUTHORIZATION, admin)).andExpect(status().isNoContent());
        mvc.perform(get(API + "/productos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void noPermiteCodigoRepetidoNiOfertaMayorAlPrecio() throws Exception {
        String admin = bearer(token("admin@duoc.cl", "admin123"));

        mvc.perform(post(API + "/productos").header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"BEB-001\",\"nombre\":\"X\",\"precio\":100,\"stock\":1,\"stockCritico\":1,\"categoriaId\":1}"))
                .andExpect(status().isConflict());

        mvc.perform(post(API + "/productos").header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"NUE-001\",\"nombre\":\"X\",\"precio\":100,\"precioOferta\":200,\"stock\":1,\"stockCritico\":1,\"categoriaId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.precioOferta").exists());
    }

    @Test
    void noSeEliminaUnaCategoriaConProductos() throws Exception {
        String admin = bearer(token("admin@duoc.cl", "admin123"));

        mvc.perform(delete(API + "/categorias/1").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("tiene productos")));
    }

    @Test
    void unAdministradorNoPuedeEliminarSuPropiaCuenta() throws Exception {
        String admin = bearer(token("admin@duoc.cl", "admin123"));

        mvc.perform(delete(API + "/usuarios/1").header(HttpHeaders.AUTHORIZATION, admin)).andExpect(status().isConflict());
        mvc.perform(get(API + "/usuarios/3/boletas").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ---------- Compra ----------

    @Test
    void unInvitadoPuedeComprarYConSesionLaBoletaQuedaAsociada() throws Exception {
        String compra = """
                {"cliente":{"nombre":"Juan","apellidos":"Pérez","correo":"juan.perez@gmail.com"},
                 "entrega":"retiro","metodoPago":"debito","items":[{"productoId":2,"cantidad":2}]}""";

        mvc.perform(post(API + "/boletas").contentType(MediaType.APPLICATION_JSON).content(compra))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("pagada"))
                .andExpect(jsonPath("$.usuarioId").doesNotExist())
                .andExpect(jsonPath("$.total").value(2980));

        String cliente = token("juan.perez@gmail.com", "juan1234");
        mvc.perform(post(API + "/boletas").header(HttpHeaders.AUTHORIZATION, bearer(cliente))
                        .contentType(MediaType.APPLICATION_JSON).content(compra))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(3));
    }

    @Test
    void laCompraValidaLosDatosDelCliente() throws Exception {
        mvc.perform(post(API + "/boletas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cliente\":{\"nombre\":\"\",\"apellidos\":\"\",\"correo\":\"malo\"},\"entrega\":\"retiro\",\"metodoPago\":\"debito\",\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores['cliente.correo']").exists())
                .andExpect(jsonPath("$.errores.items").exists());
    }

    @Test
    void swaggerDocumentaLaApi() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API MiniMarket DSY"))
                .andExpect(jsonPath("$.paths['/api/v1/productos']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }
}
