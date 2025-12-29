(function () {
    const STAR_COUNT = 4200;

    function initSpaceBackground() {
        if (!window.THREE) {
            return;
        }

        const canvas = document.getElementById('space-bg');
        if (!canvas) {
            return;
        }

        const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true });
        renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
        renderer.setClearColor(0x000000, 0);

        const scene = new THREE.Scene();
        const camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000);
        camera.position.z = 6;

        const positions = new Float32Array(STAR_COUNT * 3);
        for (let i = 0; i < STAR_COUNT; i++) {
            const i3 = i * 3;
            positions[i3] = (Math.random() - 0.5) * 320;
            positions[i3 + 1] = (Math.random() - 0.5) * 320;
            positions[i3 + 2] = (Math.random() - 0.5) * 320;
        }

        const starGeometry = new THREE.BufferGeometry();
        starGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));

        const starMaterial = new THREE.PointsMaterial({
            color: 0xffffff,
            size: 1.2,
            sizeAttenuation: true,
            transparent: true,
            opacity: 0.92,
            blending: THREE.AdditiveBlending
        });

        const stars = new THREE.Points(starGeometry, starMaterial);
        scene.add(stars);

        const nebulaPrimary = createNebula(0x3a7bd5, 0.16, -35);
        nebulaPrimary.rotation.z = Math.random() * Math.PI;
        const nebulaSecondary = createNebula(0xa8d8f0, 0.12, -45);
        scene.add(nebulaPrimary);
        scene.add(nebulaSecondary);

        function createNebula(color, opacity, depth) {
            const geometry = new THREE.PlaneGeometry(200, 200);
            const material = new THREE.MeshBasicMaterial({
                color,
                transparent: true,
                opacity,
                blending: THREE.AdditiveBlending
            });
            const mesh = new THREE.Mesh(geometry, material);
            mesh.position.z = depth;
            return mesh;
        }

        function resize() {
            const width = window.innerWidth;
            const height = window.innerHeight;
            renderer.setSize(width, height);
            camera.aspect = width / height;
            camera.updateProjectionMatrix();
        }

        window.addEventListener('resize', resize);
        resize();

        function animate() {
            stars.rotation.y += 0.00095;
            stars.rotation.x += 0.0003;
            nebulaPrimary.rotation.z += 0.00012;
            nebulaSecondary.rotation.z -= 0.00008;
            camera.position.z = 6 + Math.sin(Date.now() * 0.00025) * 0.4;

            renderer.render(scene, camera);
            requestAnimationFrame(animate);
        }

        animate();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initSpaceBackground);
    } else {
        initSpaceBackground();
    }
})();
