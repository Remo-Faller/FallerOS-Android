// Faller'OS — Service Worker
// Cache-first dla plików systemowych (działanie offline), sieć dla zasobów zewnętrznych (np. tłumaczenia).

const CACHE_NAME = 'fallers-os-cache-v1';
const CORE_ASSETS = [
  './',
  './index.html',
  './manifest.json',
  './icon-192.png',
  './icon-512.png',
  './icon-maskable-192.png',
  './icon-maskable-512.png',
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => cache.addAll(CORE_ASSETS))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
    ).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const req = event.request;
  const url = new URL(req.url);

  // Tylko żądania GET z tej samej domeny są obsługiwane offline.
  if (req.method !== 'GET' || url.origin !== self.location.origin) {
    return; // przepuść do sieci bez ingerencji (np. API tłumaczenia)
  }

  event.respondWith(
    caches.match(req).then((cached) => {
      const network = fetch(req).then((res) => {
        if (res && res.status === 200) {
          const copy = res.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(req, copy));
        }
        return res;
      }).catch(() => cached || caches.match('./index.html'));
      // strategia stale-while-revalidate: serwuj z cache natychmiast, odśwież w tle
      return cached || network;
    })
  );
});
