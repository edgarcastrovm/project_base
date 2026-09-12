#!/usr/bin/env bash
# ============================================================================
# Generador rapido de Repository / Service / ServiceImpl / Controller
#
# Uso (Git Bash en Windows 11, o cualquier bash en Linux/Mac):
#   cd backend/scripts
#   ./generar-crud.sh
#
# Que hace:
#   1) Lee la lista de entidades definida mas abajo (ENTIDADES).
#   2) Crea las carpetas de los paquetes si no existen.
#   3) Por cada entidad genera (si el archivo NO existe todavia):
#        - Repository (interface, extiende JpaRepository)
#        - Service (interface) con: listar, listarPorId, actualizar, eliminar (logico)
#        - ServiceImpl (implementacion basica, con TODOs donde corresponde)
#        - Controller (expone los 4 metodos del service como endpoints REST)
#
#   Si un archivo ya existe, se omite para no pisar cambios manuales.
#   Borra el archivo que quieras y vuelve a correr el script para regenerarlo.
# ============================================================================

set -euo pipefail

# ----------------------------------------------------------------------------
# 1) ENTIDADES: formato "NombreClase:TipoDeId"
#    El nombre debe ser EXACTO al de la clase en /entity (con el prefijo Tbl).
#    El tipo de id debe coincidir con el @Id de la entidad (Long, Integer, etc).
#    Agrega/quita lineas segun necesites.
# ----------------------------------------------------------------------------
ENTIDADES=(
  #"TblUser:Long"
  #"TblRol:Long"
  "TblPersona:Long"
)

# ----------------------------------------------------------------------------
# 2) Paquetes del proyecto (se crean las carpetas si no existen)
# ----------------------------------------------------------------------------
PAQUETE_BASE="com.uisrael.backend"
PAQUETE_ENTITY="${PAQUETE_BASE}.entity"
PAQUETE_REPOSITORY="${PAQUETE_BASE}.repository"
PAQUETE_SERVICE="${PAQUETE_BASE}.service"
PAQUETE_SERVICE_IMPL="${PAQUETE_BASE}.service.impl"
PAQUETE_CONTROLLER="${PAQUETE_BASE}.controller"

# ----------------------------------------------------------------------------
# Rutas de carpetas (independiente de donde se ejecute el script)
# ----------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
SRC_BASE="${BACKEND_ROOT}/src/main/java/$(echo "${PAQUETE_BASE}" | tr '.' '/')"

DIR_REPOSITORY="${SRC_BASE}/repository"
DIR_SERVICE="${SRC_BASE}/service"
DIR_SERVICE_IMPL="${SRC_BASE}/service/impl"
DIR_CONTROLLER="${SRC_BASE}/controller"

mkdir -p "${DIR_REPOSITORY}" "${DIR_SERVICE}" "${DIR_SERVICE_IMPL}" "${DIR_CONTROLLER}"

# Crea un archivo solo si no existe (para no pisar cambios manuales)
crear_archivo() {
  local ruta="$1"
  local contenido="$2"
  if [ -f "${ruta}" ]; then
    echo "  - ya existe, se omite: ${ruta#${BACKEND_ROOT}/}"
  else
    printf '%s' "${contenido}" > "${ruta}"
    echo "  - creado: ${ruta#${BACKEND_ROOT}/}"
  fi
}

# ----------------------------------------------------------------------------
# 3) Generacion por cada entidad
# ----------------------------------------------------------------------------
for item in "${ENTIDADES[@]}"; do
  ENTIDAD="${item%%:*}"     # ej: TblPersona
  ID_TIPO="${item##*:}"    # ej: Integer

  NOMBRE="${ENTIDAD#Tbl}"  # quita el prefijo "Tbl" -> Persona
  PRIMERA_LETRA="$(printf '%s' "${NOMBRE:0:1}" | tr '[:upper:]' '[:lower:]')"
  VAR="${PRIMERA_LETRA}${NOMBRE:1}"                                   # persona (para variables)
  RUTA_REST="$(printf '%s' "${NOMBRE}" | tr '[:upper:]' '[:lower:]')" # persona (para /api/persona)

  echo ""
  echo "==> ${ENTIDAD} (${NOMBRE})"

  # ---------- Repository ----------
  REPO_CONTENIDO=$(cat <<EOF
package ${PAQUETE_REPOSITORY};

import ${PAQUETE_ENTITY}.${ENTIDAD};
import org.springframework.data.jpa.repository.JpaRepository;

public interface I${NOMBRE}Repository extends JpaRepository<${ENTIDAD}, ${ID_TIPO}> {
}
EOF
)
  crear_archivo "${DIR_REPOSITORY}/I${NOMBRE}Repository.java" "${REPO_CONTENIDO}"

  # ---------- Service (interface) ----------
  SERVICE_CONTENIDO=$(cat <<EOF
package ${PAQUETE_SERVICE};

import ${PAQUETE_ENTITY}.${ENTIDAD};

import java.util.List;

public interface I${NOMBRE}Service {

    List<${ENTIDAD}> listar();

    ${ENTIDAD} listarPorId(${ID_TIPO} id);

    ${ENTIDAD} actualizar(${ID_TIPO} id, ${ENTIDAD} datos);

    void eliminar(${ID_TIPO} id);
}
EOF
)
  crear_archivo "${DIR_SERVICE}/I${NOMBRE}Service.java" "${SERVICE_CONTENIDO}"

  # ---------- ServiceImpl ----------
  SERVICE_IMPL_CONTENIDO=$(cat <<EOF
package ${PAQUETE_SERVICE_IMPL};

import ${PAQUETE_ENTITY}.${ENTIDAD};
import ${PAQUETE_REPOSITORY}.I${NOMBRE}Repository;
import ${PAQUETE_SERVICE}.I${NOMBRE}Service;
import com.uisrael.backend.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ${NOMBRE}ServiceImpl implements I${NOMBRE}Service {

    private final I${NOMBRE}Repository ${VAR}Repository;

    @Override
    @Transactional(readOnly = true)
    public List<${ENTIDAD}> listar() {
        return ${VAR}Repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ${ENTIDAD} listarPorId(${ID_TIPO} id) {
        return ${VAR}Repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("${NOMBRE} no encontrado con id " + id));
    }

    @Override
    @Transactional
    public ${ENTIDAD} actualizar(${ID_TIPO} id, ${ENTIDAD} datos) {
        ${ENTIDAD} existente = listarPorId(id);
        // TODO: copiar aqui los campos editables de "datos" hacia "existente"
        return ${VAR}Repository.save(existente);
    }

    @Override
    @Transactional
    public void eliminar(${ID_TIPO} id) {
        ${ENTIDAD} existente = listarPorId(id);
        // TODO: eliminado logico -> reemplazar por el campo real de estado
        // de la entidad, ej: existente.setActivo(false);
        ${VAR}Repository.save(existente);
    }
}
EOF
)
  crear_archivo "${DIR_SERVICE_IMPL}/${NOMBRE}ServiceImpl.java" "${SERVICE_IMPL_CONTENIDO}"

  # ---------- Controller ----------
  CONTROLLER_CONTENIDO=$(cat <<EOF
package ${PAQUETE_CONTROLLER};

import ${PAQUETE_ENTITY}.${ENTIDAD};
import ${PAQUETE_SERVICE}.I${NOMBRE}Service;
import com.uisrael.backend.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${RUTA_REST}")
@RequiredArgsConstructor
public class ${NOMBRE}Controller {

    private final I${NOMBRE}Service ${VAR}Service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<${ENTIDAD}>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("${NOMBRE} listado correctamente", ${VAR}Service.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<${ENTIDAD}>> listarPorId(@PathVariable ${ID_TIPO} id) {
        return ResponseEntity.ok(ApiResponse.ok("${NOMBRE} encontrado", ${VAR}Service.listarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<${ENTIDAD}>> actualizar(@PathVariable ${ID_TIPO} id, @RequestBody ${ENTIDAD} datos) {
        return ResponseEntity.ok(ApiResponse.ok("${NOMBRE} actualizado correctamente", ${VAR}Service.actualizar(id, datos)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable ${ID_TIPO} id) {
        ${VAR}Service.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("${NOMBRE} eliminado correctamente", null));
    }
}
EOF
)
  crear_archivo "${DIR_CONTROLLER}/${NOMBRE}Controller.java" "${CONTROLLER_CONTENIDO}"

done

echo ""
echo "Listo. Revisa los TODO dentro de cada *ServiceImpl.java antes de compilar:"
echo "  - actualizar(): copiar los campos que realmente se deben editar."
echo "  - eliminar(): usar el campo de estado real de la entidad (ej. activo)."
