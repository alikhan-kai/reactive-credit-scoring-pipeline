import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// Конфигурация профиля нагрузки (стресс-тест "Kaspi Жұма")
export const options = {
    stages: [
        { duration: '30s', target: 500 },   // Плавный разгон до 500 RPS
        { duration: '1m', target: 1500 },   // Выход на среднюю нагрузку
        { duration: '2m', target: 3000 },   // Пиковая нагрузка "Kaspi Жұма" (3 000 RPS)
        { duration: '30s', target: 0 },     // Остывание
    ],
    thresholds: {
        // Жесткий банковский SLA: 99% всех запросов должны выполняться быстрее 2 секунд!
        http_req_duration: ['p(95)<1200', 'p(99)<2000'],
        // Допустимый процент сетевых ошибок - менее 1%
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';

// Пул синтетических ИИН Казахстана под разные риск-профили
const TEST_IINS = [
    '950315350127', // PRIME: Одобрение
    '900115350128', // PRIME: Высокий доход
    '920520350126', // AVERAGE: Стандартный клиент
    '940810350123', // OVERLEVERAGED: Counter-Offer
    '881125350129', // DEFAULTER: Отказ ПКБ
    '850330350122', // LEGAL_BLOCK: Отказ АИС ОИП
];

export default function () {
    // Выбираем случайный ИИН и случайную запрашиваемую сумму
    const iin = TEST_IINS[Math.floor(Math.random() * TEST_IINS.length)];
    const requestedAmount = Math.floor(Math.random() * (1500000 - 50000 + 1)) + 50000;
    const termMonths = [3, 6, 12, 24][Math.floor(Math.random() * 4)];
    const productType = Math.random() > 0.3 ? 'INSTALLMENT_0_0_12' : 'EXPRESS_CREDIT';

    const payload = JSON.stringify({
        iin: iin,
        requestedAmount: requestedAmount,
        termMonths: termMonths,
        productType: productType,
        phoneNumber: '+77011234567',
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'X-Idempotency-Key': uuidv4(), // Уникальный ключ идемпотентности на каждый запрос
        },
    };

    const res = http.post(`${BASE_URL}/v1/credit/apply`, payload, params);

    // Проверяем статус ответа (200 OK при одобрении/отказе/встречном предложении)
    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 2s (SLA)': (r) => r.timings.duration < 2000,
    });

    sleep(0.1); // Небольшая пауза между тактами виртуального пользователя
}