# 🎨 Estructura de CSS

Esta aplicación utiliza una estructura de CSS modular y escalable. Aquí tienes una guía completa de dónde poner cada tipo de estilo.

## 📁 Estructura de Carpetas

```
src/
├── styles/
│   ├── index.css              # Punto de entrada que importa todos los estilos
│   ├── variables.css          # Variables CSS globales (colores, espaciado, etc.)
│   ├── components.css         # Componentes reutilizables (botones, cards, forms)
│   ├── layout.css            # Estilos de layout y estructura general
│   └── components/
│       ├── products.css       # Estilos específicos para productos
│       ├── orders.css         # Estilos específicos para órdenes
│       └── [feature].css      # Otros estilos específicos por característica
├── components/
│   └── [Component]/
│       ├── Component.tsx
│       └── Component.module.css  # Estilos específicos del componente (opcional)
└── App.css                   # Solo si necesitas estilos muy específicos del App
```

## 🎯 Dónde Poner Cada Tipo de CSS

### 1. **Variables CSS** → `src/styles/variables.css`

- Colores del tema
- Espaciado consistente
- Tipografía
- Sombras y bordes
- Transiciones

```css
:root {
  --primary-color: #007bff;
  --spacing-md: 16px;
  --border-radius: 8px;
}
```

### 2. **Componentes Reutilizables** → `src/styles/components.css`

- Botones (.btn, .btn-primary, .btn-secondary)
- Cards (.card, .card-header, .card-body)
- Formularios (.form-input, .form-label)
- Alertas (.alert, .alert-success)
- Modales (.modal, .modal-overlay)
- Utilidades (.d-flex, .text-center, .mb-3)

### 3. **Layout General** → `src/styles/layout.css`

- Estructura principal (.app-container, .main-content)
- Grids y layouts responsivos
- Navegación
- Headers y footers

### 4. **Características Específicas** → `src/styles/components/[feature].css`

- `products.css`: Todo relacionado con productos
- `orders.css`: Todo relacionado con órdenes
- `auth.css`: Estilos de autenticación (si necesitas)

### 5. **Estilos de Componente** → `Component.module.css` (opcional)

- Estilos muy específicos de un solo componente
- Cuando necesites CSS Modules para evitar conflictos
- Animaciones complejas específicas del componente

## 🚀 Cómo Usar

### Importar Estilos Globales

En tu `main.tsx`:

```tsx
import "./styles/index.css";
```

### Usar Clases en Componentes

```tsx
// Componentes reutilizables
<button className="btn btn-primary">Guardar</button>
<div className="card">...</div>

// Layouts
<div className="app-container">
  <main className="main-content">
    <div className="container">...</div>
  </main>
</div>

// Características específicas
<div className="products-container">
  <div className="product-card">...</div>
</div>
```

### Variables CSS en Componentes

```css
.mi-componente {
  padding: var(--spacing-md);
  color: var(--primary-color);
  border-radius: var(--border-radius);
}
```

## 🎨 Sistema de Clases

### Botones

- `.btn` - Clase base
- `.btn-primary`, `.btn-secondary`, `.btn-success`, `.btn-danger`
- `.btn-sm`, `.btn-lg` - Tamaños

### Espaciado

- `.mb-0` a `.mb-5` - Margin bottom
- `.mt-0` a `.mt-5` - Margin top
- `.gap-sm`, `.gap-md`, `.gap-lg` - Gap en flexbox

### Layout

- `.d-flex` - Display flex
- `.justify-center`, `.justify-between` - Justify content
- `.align-center` - Align items
- `.text-center`, `.text-left`, `.text-right` - Text align

## 📱 Responsivo

Todos los estilos incluyen breakpoints responsivos:

- Desktop: `> 768px`
- Tablet: `<= 768px`
- Mobile: `<= 480px`

## 🌙 Tema Oscuro

El sistema soporta automáticamente tema oscuro usando:

```css
@media (prefers-color-scheme: dark) {
  :root {
    --bg-primary: #1a1a1a;
    --text-primary: rgba(255, 255, 255, 0.87);
  }
}
```

## 🛠️ Consejos

1. **Usa variables CSS** para mantener consistencia
2. **Reutiliza clases de componentes** antes de crear nuevas
3. **Agrupa estilos relacionados** en el mismo archivo
4. **Mantén especificidad baja** para facilitar override
5. **Usa nombres descriptivos** para las clases
6. **Incluye estados hover/focus** en elementos interactivos

## 📝 Ejemplo de Nuevo Componente

Si vas a crear un nuevo componente de "Dashboard":

1. **Estilos específicos** → `src/styles/components/dashboard.css`
2. **Importar en** → `src/styles/index.css`
3. **Usar en el componente**:

```tsx
// Dashboard.tsx
export function Dashboard() {
  return (
    <div className="dashboard-container">
      <div className="card">
        <div className="card-header">
          <h2>Mi Dashboard</h2>
        </div>
        <div className="card-body">
          <button className="btn btn-primary">Acción Principal</button>
        </div>
      </div>
    </div>
  );
}
```

¡Esta estructura te permitirá mantener tus estilos organizados y escalables! 🎉
