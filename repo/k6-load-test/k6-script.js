import http from 'k6/http';
import { sleep } from  'k6';
import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';

const BASE_URL = 'https://predawn-days.com';

export const options = {
    stages: [
        {duration: '10m', target: 50}
    ],
};

const users = Array.from({ length: 100 }, (_, i) => ({
    username: `user${i + 1}`,
    password: '1234',
}));

export function setup() {
    return { users };
}

export default function (data) {
    const vuIndex = (__VU - 1) % data.users.length;
    const user = data.users[vuIndex];

    let jar = http.cookieJar();

    if (!jar.cookiesForURL(BASE_URL).JSESSIONID) {

        const loginRes = http.post(`${BASE_URL}/login`,
            {
            loginId: user.username,
            password: user.password,
            },
            { jar: jar }
        );
    }

    const random = Math.random();

    if (random < 0.05) {
        const formData = new FormData();

        formData.append('categoryName', 'GENERAL');
        formData.append('boardOpen', 'ALL');
        formData.append('title', `제목 ${Math.floor(Math.random() * 10000)}`);
        formData.append('content', `내용 ${Math.floor(Math.random() * 10000)}`);

        http.post(
            `${BASE_URL}/api/board/write`,
            formData.body(),
            {
                headers: {
                    'Content-Type': 'multipart/form-data; boundary=' + formData.boundary,
                },
                jar: jar,
            }
        );
    } else {
        http.get(`${BASE_URL}/boards?searchType=&keyword=&sortType=latest`, { jar: jar });
    }

    sleep(1);

}