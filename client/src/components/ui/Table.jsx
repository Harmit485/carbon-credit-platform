import { cn } from '../../utils/cn';
import Tag from './Tag';

const Table = ({
  columns,
  data,
  emptyState = 'No records found',
  className,
  rowKey = (row) => row.id,
  maxHeight = 'max-h-96' // Default max height for scrolling
}) => (
  <div className={cn('glass-panel overflow-hidden', className)}>
    <div className={cn('overflow-auto -mx-4 sm:mx-0 px-4 sm:px-0', maxHeight)}>
      <table className="glass-table min-w-full">
        <thead className="sticky top-0 z-10" style={{ background: 'var(--color-surface-strong)', backdropFilter: 'blur(var(--blur-strong))' }}>
          <tr>
            {columns.map((column) => (
              <th key={column.accessor} className="text-left">
                {column.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="py-10 text-center" style={{ color: 'var(--color-text-muted)' }}>
                {emptyState}
              </td>
            </tr>
          ) : (
            data.map((row) => (
              <tr key={rowKey(row)}>
                {columns.map((column) => (
                  <td key={column.accessor}>
                    {column.render
                      ? column.render(row[column.accessor], row)
                      : formatCell(column.type, row[column.accessor])}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  </div>
);

const formatCell = (type, value) => {
  if (type === 'currency') {
    return `₹${Number(value || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
  }

  if (type === 'status') {
    return <Tag variant={value === 'ACTIVE' ? 'success' : 'warning'}>{value}</Tag>;
  }

  if (type === 'date') {
    return value ? new Date(value).toLocaleDateString() : '—';
  }

  return value ?? '—';
};

export default Table;


