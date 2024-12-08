if ('serviceWorker' in navigator && 'PushManager' in window) {
    navigator.serviceWorker.register('/service-worker.js')
        .then(function(registration) {
            console.log('ServiceWorker registered');

            return registration.pushManager.subscribe({
                userVisibleOnly: true,
                applicationServerKey: 'YOUR_PUBLIC_VAPID_KEY'
            });
        })
        .then(function(subscription) {
            // 서버에 구독 정보 전송
            fetch('/api/notifications/subscribe', {
                method: 'POST',
                body: JSON.stringify(subscription),
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        })
        .catch(function(error) {
            console.log('Service Worker error:', error);
        });
}