document.addEventListener("DOMContentLoaded", function () {

    const filterButtons = document.querySelectorAll(".care-filter-btn");
    const taskCards = document.querySelectorAll(".care-task-card");

    filterButtons.forEach(button => {

        button.addEventListener("click", function () {

            // เปลี่ยนปุ่มที่ active
            filterButtons.forEach(btn => {
                btn.classList.remove("active");
            });

            this.classList.add("active");

            // อ่านประเภทที่เลือก
            const filter = this.dataset.filter;

            // แสดง/ซ่อนรายการ
            taskCards.forEach(card => {

                const action = card.dataset.action;

                if (filter === "ALL" || action === filter) {
                    card.style.display = "flex";
                } else {
                    card.style.display = "none";
                }

            });

        });

    });

});