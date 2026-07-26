(function () {
  if (window.TransactionDonutCharts) return;

  function formatMoney(value, locale) {
    return `${Number(value || 0).toLocaleString(locale || 'vi-VN')} VND`;
  }

  function loadImage(src) {
    return new Promise((resolve) => {
      if (!src) return resolve(null);
      const img = new Image();
      img.onload = () => resolve(img);
      img.onerror = () => resolve(null);
      img.src = src;
    });
  }

  const badgePlugin = {
    id: 'txDoughnutBadgePlugin',
    afterDatasetsDraw(chart) {
      const meta = chart.getDatasetMeta(0);
      const dataset = chart.data.datasets[0];
      const total = (dataset?.data || []).reduce((sum, value) => sum + Number(value || 0), 0);
      if (!meta?.data?.length || !total) return;

      const ctx = chart.ctx;
      ctx.save();
      ctx.lineJoin = 'round';
      ctx.lineCap = 'round';

      meta.data.forEach((arc, index) => {
        const value = Number(dataset.data[index] || 0);
        if (!value) return;

        const angle = (arc.startAngle + arc.endAngle) / 2;
        const cos = Math.cos(angle);
        const sin = Math.sin(angle);
        const color = Array.isArray(dataset.backgroundColor) ? dataset.backgroundColor[index] : '#696cff';

        const startX = arc.x + cos * (arc.outerRadius - 2);
        const startY = arc.y + sin * (arc.outerRadius - 2);
        const elbowX = arc.x + cos * (arc.outerRadius + 24);
        const elbowY = arc.y + sin * (arc.outerRadius + 24);
        const badgeRadius = 16;
        const badgeOffset = 42;
        const rawBadgeX = elbowX + (cos >= 0 ? badgeOffset : -badgeOffset);
        const badgeY = elbowY;
        const minBadgeX = badgeRadius + 42;
        const maxBadgeX = chart.width - badgeRadius - 42;
        const badgeX = Math.max(minBadgeX, Math.min(maxBadgeX, rawBadgeX));
        const lineEndX = badgeX + (cos >= 0 ? -badgeRadius - 4 : badgeRadius + 4);
        const lineEndY = badgeY;

        ctx.beginPath();
        ctx.strokeStyle = 'rgba(255,255,255,0.98)';
        ctx.lineWidth = 6;
        ctx.moveTo(startX, startY);
        ctx.lineTo(elbowX, elbowY);
        ctx.lineTo(lineEndX, lineEndY);
        ctx.stroke();

        ctx.beginPath();
        ctx.strokeStyle = color;
        ctx.lineWidth = 2.5;
        ctx.moveTo(startX, startY);
        ctx.lineTo(elbowX, elbowY);
        ctx.lineTo(lineEndX, lineEndY);
        ctx.stroke();

        ctx.beginPath();
        ctx.fillStyle = color;
        ctx.arc(elbowX, elbowY, 3.5, 0, Math.PI * 2);
        ctx.fill();
        ctx.lineWidth = 2;
        ctx.strokeStyle = '#fff';
        ctx.stroke();

        ctx.beginPath();
        ctx.fillStyle = '#fff';
        ctx.shadowColor = 'rgba(67, 89, 113, 0.16)';
        ctx.shadowBlur = 12;
        ctx.arc(badgeX, badgeY, badgeRadius, 0, Math.PI * 2);
        ctx.fill();
        ctx.shadowBlur = 0;

        ctx.beginPath();
        ctx.strokeStyle = 'rgba(255,255,255,0.98)';
        ctx.lineWidth = 5;
        ctx.arc(badgeX, badgeY, badgeRadius, 0, Math.PI * 2);
        ctx.stroke();

        ctx.beginPath();
        ctx.strokeStyle = color;
        ctx.lineWidth = 2;
        ctx.arc(badgeX, badgeY, badgeRadius, 0, Math.PI * 2);
        ctx.stroke();

        const iconImage = chart.$iconImages?.[index];
        if (iconImage) {
          const size = 18;
          ctx.drawImage(iconImage, badgeX - size / 2, badgeY - size / 2, size, size);
        } else {
          ctx.fillStyle = color;
          ctx.font = '700 12px Inter, system-ui, sans-serif';
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';
          ctx.fillText('%', badgeX, badgeY + 0.5);
        }

        const percent = `${((value / total) * 100).toFixed(1).replace('.0', '')}%`;
        ctx.font = '700 12px Inter, system-ui, sans-serif';
        ctx.fillStyle = color;
        ctx.textAlign = cos >= 0 ? 'left' : 'right';
        ctx.textBaseline = 'middle';
        const labelGap = 20;
        const labelWidth = ctx.measureText(percent).width;
        const textX = cos >= 0
          ? Math.min(chart.width - labelWidth - 10, badgeX + labelGap)
          : Math.max(labelWidth + 10, badgeX - labelGap);
        ctx.lineWidth = 3.5;
        ctx.strokeStyle = 'rgba(255,255,255,0.98)';
        ctx.strokeText(percent, textX, badgeY);
        ctx.fillText(percent, textX, badgeY);
      });

      ctx.restore();
    }
  };

  async function render(config) {
    const canvas = config?.canvas;
    if (!canvas || typeof Chart === 'undefined') return null;

    const items = Array.isArray(config.items) ? config.items.filter((x) => Number(x.value || 0) > 0) : [];
    if (config.chart && typeof config.chart.destroy === 'function') {
      config.chart.destroy();
    }

    if (!items.length) {
      const emptyChart = new Chart(canvas, {
        type: 'doughnut',
        data: {
          labels: ['Empty'],
          datasets: [{
            data: [1],
            backgroundColor: ['#edf1f7'],
            borderWidth: 0,
            hoverOffset: 0,
            spacing: 0,
            borderRadius: 0
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '68%',
          plugins: { legend: { display: false }, tooltip: { enabled: false } }
        }
      });
      return emptyChart;
    }

    const iconImages = await Promise.all(items.map((item) => loadImage(item.icon)));

    const chart = new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels: items.map((item) => item.name),
        datasets: [{
          data: items.map((item) => item.value),
          backgroundColor: items.map((item) => item.color),
          borderColor: '#ffffff',
          borderWidth: 4,
          borderRadius: 0,
          hoverOffset: 6,
          spacing: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '66%',
        layout: {
          padding: (() => {
            const width = canvas.parentElement?.clientWidth || canvas.clientWidth || 420;
            if (width < 340) return { top: 16, right: 64, bottom: 36, left: 64 };
            if (width < 420) return { top: 20, right: 92, bottom: 40, left: 92 };
            return { top: 22, right: 124, bottom: 42, left: 124 };
          })()
        },
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (ctx) => `${ctx.label}: ${formatMoney(ctx.raw, config.locale)}`
            }
          },
          txDoughnutBadgePlugin: true
        }
      },
      plugins: [badgePlugin]
    });

    chart.$iconImages = iconImages;
    chart.update();
    return chart;
  }

  window.TransactionDonutCharts = { render };
})();
