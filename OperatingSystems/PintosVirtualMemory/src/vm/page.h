#ifndef VM_PAGE_H
#define VM_PAGE_H

#include <hash.h>
#include "devices/block.h"
#include "filesys/off_t.h"
#include "threads/synch.h"

/* Virtual page. */
struct page {

	struct hash_elem hash_elem;
	void *addr; /* User virtual address. */
	struct frame *frame; /* Page frame. */
	struct file *file;
	int32_t offsetAddress;
	//uint8_t virtualAddress;
	uint32_t readBytes;
	uint32_t zeroBytes;
	bool isWritable;
/* ...............          other struct members as necessary */
};

void page_exit(void);

//struct page *page_allocate(void *, bool read_only);

struct page * page_allocate(struct file *, int32_t, void *, uint32_t, uint32_t, bool);

void initializePageTable(struct hash *);

//struct page *
//page_allocate(void *vaddr, bool read_only) {
//page_allocate(struct file *, int32_t, uint8_t *, uint32_t, uint32_t, bool);

struct page * page_for_addr(const void *);

void page_deallocate(void *vaddr);

bool page_in(void *fault_addr);
bool page_out(struct page *);
bool page_accessed_recently(struct page *);

bool page_lock(const void *, bool will_write);
void page_unlock(const void *);

//unsigned page_hash(const struct hash_elem *, void *aux);
//bool page_less(const struct hash_elem *, const struct hash_elem *, void *aux);

hash_hash_func page_hash;
hash_less_func page_less;

#endif /* vm/page.h */
