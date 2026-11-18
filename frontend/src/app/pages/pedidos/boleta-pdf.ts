import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import type { Pedido } from '../../models/carrito-pedido.model';

const moneda = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const fecha = new Intl.DateTimeFormat('es-PE', { dateStyle: 'short', timeStyle: 'short' });

function soles(monto: number): string {
  return `S/ ${moneda.format(monto)}`;
}

export function guardarBoletaPdf(pedido: Pedido, metodo: string): void {
  if (!pedido.fechaPago) throw new Error('El pedido no tiene fecha de pago.');

  const doc = new jsPDF();
  doc.setFontSize(19);
  doc.text(`MHM Store | Boleta simulada B-${pedido.id}`, 14, 18);
  doc.setFontSize(10);
  doc.text('Comprobante de demostración, sin validez tributaria.', 14, 25);
  doc.text(`Pedido #${pedido.id} | Pago: ${fecha.format(new Date(pedido.fechaPago))}`, 14, 34);
  doc.text(`Método: ${metodo}`, 14, 40);

  const cliente = doc.splitTextToSize(`Cliente: ${pedido.usuarioEmail}`, 182);
  doc.text(cliente, 14, 46);

  autoTable(doc, {
    startY: 49 + cliente.length * 4,
    head: [['Producto', 'Cantidad', 'Precio unitario', 'Subtotal']],
    body: pedido.items.map(item => [
      `${item.productoModelo} (${item.productoCodigo})`,
      item.cantidad,
      soles(item.precioUnitario),
      soles(item.subtotal)
    ]),
    foot: [[{ content: 'Total pagado', colSpan: 3, styles: { halign: 'right' } }, soles(pedido.total)]],
    showFoot: 'lastPage',
    headStyles: { fillColor: [23, 25, 27] },
    footStyles: { fillColor: [23, 25, 27], halign: 'right' },
    columnStyles: { 1: { halign: 'right' }, 2: { halign: 'right' }, 3: { halign: 'right' } }
  });

  doc.save(`boleta-B-${pedido.id}.pdf`);
}
