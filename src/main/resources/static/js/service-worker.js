self.addEventListener('push', function(event) {
    const options = {
        body: event.data.text(),
        icon: '/images/icon.png',
        badge: '/images/badge.png'
    };

    event.waitUntil(
        self.registration.showNotification('미세먼지 경보', options)
    );
});