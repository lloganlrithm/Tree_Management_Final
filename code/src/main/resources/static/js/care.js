document.addEventListener("DOMContentLoaded", function () {

    const filterButtons = document.querySelectorAll(".care-filters .chip");
    const taskCards = document.querySelectorAll(".care-task-card");
    const groups = document.querySelectorAll(".care-group");
    const totalCount = document.getElementById("careTotalCount");
    const filterEmpty = document.getElementById("careFilterEmpty");

    filterButtons.forEach(button => {

        button.addEventListener("click", function () {

            // เปลี่ยนปุ่มที่ active
            filterButtons.forEach(btn => btn.classList.remove("active"));
            this.classList.add("active");

            // อ่านประเภทที่เลือก
            const filter = this.dataset.filter;

            // แสดง/ซ่อนการ์ด
            taskCards.forEach(card => {
                const match = filter === "ALL" || card.dataset.action === filter;
                card.style.display = match ? "flex" : "none";
            });

            // นับใหม่ทีละกลุ่ม (เลยกำหนด / วันนี้ / ถัดไป) ซ่อนกลุ่มที่ไม่เหลือการ์ด
            let total = 0;
            groups.forEach(group => {
                const visible = group.querySelectorAll('.care-task-card:not([style*="none"])').length;
                total += visible;

                group.style.display = visible > 0 ? "" : "none";

                const groupCount = group.querySelector(".care-group-count");
                if (groupCount) groupCount.textContent = visible;
            });

            if (totalCount) totalCount.textContent = total + " รายการ";

            // มีตารางอยู่ แต่ประเภทที่เลือกไม่มีเลย
            if (filterEmpty) filterEmpty.hidden = !(taskCards.length > 0 && total === 0);

        });

    });

});