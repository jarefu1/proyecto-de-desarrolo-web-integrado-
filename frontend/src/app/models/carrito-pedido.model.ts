export type EstadoCarrito = 'ACTIVO' | 'ENVIADO_A_CAJA' | 'CANCELADO';
export type EstadoPedido = 'PENDIENTE' | 'PROCESADO' | 'COMPLETADO';

export interface ItemCarrito {
  id: number;
  productoId: number;
  productoCodigo: string;
  productoModelo: string;
  productoImagenUrl?: string | null;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Carrito {
  id: number;
  usuarioId: number;
  usuarioEmail: string;
  fechaCreacion: string;
  estado: EstadoCarrito;
  items: ItemCarrito[];
  total: number;
}

export interface CarritoItemRequest {
  productoId: number;
  cantidad: number;
}

export interface Pedido {
  id: number;
  carritoId: number;
  usuarioId: number;
  usuarioEmail: string;
  fechaEnvio: string;
  estado: EstadoPedido;
  total: number;
  metodoPago: string | null;
  fechaPago: string | null;
  items: ItemCarrito[];
}

export type MetodoPagoSimulado = 'TARJETA' | 'TRANSFERENCIA' | 'BILLETERA';

export interface DetalleReporte {
  id: number;
  productoId: number;
  productoCodigo: string;
  productoModelo: string;
  cantidadVendida: number;
  ingresos: number;
}

export interface ReporteVentas {
  id: number;
  fechaInicio: string;
  fechaFin: string;
  totalProductosVendidos: number;
  totalIngresos: number;
  detalles: DetalleReporte[];
}
